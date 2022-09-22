/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

/*
 * Reference from Maven: org.hibernate.validator:hibernate-validator:6.0.16.Final
 * org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator
 */

/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.xcal.api.config;

import com.xcal.api.service.DBMessageSource;
import com.xcal.api.service.I18nService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.messageinterpolation.ElTermResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTerm;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.ParameterTermResolver;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.Token;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.el.ExpressionFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Adam Stawicki
 * @author Marko Bekhta
 *
 * @since 5.2
 */
@Component("messageInterpolator")
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DBMessageInterpolator implements MessageInterpolator {
    @NonNull
    DBMessageSource messageSource;

    private static final Pattern LEFT_BRACE = Pattern.compile( "\\{", Pattern.LITERAL );
    private static final Pattern RIGHT_BRACE = Pattern.compile( "\\}", Pattern.LITERAL );
    private static final Pattern SLASH = Pattern.compile( "\\\\", Pattern.LITERAL );
    private static final Pattern DOLLAR = Pattern.compile( "\\$", Pattern.LITERAL );

    @Override
    public String interpolate(String message, Context context) {
        // probably no need for caching, but it could be done by parameters since the map
        // is immutable and uniquely built per Validation definition, the comparison has to be based on == and not equals though
        String interpolatedMessage = message;
        try {
            interpolatedMessage = interpolateMessage( message, context, Locale.getDefault() );
        }
        catch (MessageDescriptorFormatException e) {
            log.warn( e.getMessage() );
        }
        return interpolatedMessage;
    }

    @Override
    public String interpolate(String message, Context context, Locale locale) {
        String interpolatedMessage = message;
        try {
            interpolatedMessage = interpolateMessage( message, context, locale );
        }
        catch (ValidationException e) {
            log.warn( e.getMessage() );
        }
        return interpolatedMessage;
    }

    /**
     * Runs the message interpolation according to algorithm specified in the Bean Validation specification.
     * <p>
     * Note:
     * <p>
     * Look-ups in user bundles is recursive whereas look-ups in default bundle are not!
     *
     * @param message the message to interpolate
     * @param context the context for this interpolation
     * @param locale the {@code Locale} to use for the resource bundle.
     *
     * @return the interpolated message.
     */
    private String interpolateMessage(String message, Context context, Locale locale){
        String originalText = message;
        Matcher matcher = I18nService.regWithKeyPattern.matcher(message);
        while (matcher.find()) {
            originalText = matcher.group(1);
        }
        String resolvedMessage = resolveMessage( originalText, locale );
       if(I18nService.regWithKeyPattern.matcher(resolvedMessage).find()){
           //resolve EL expressions,eg ${foo}.
            resolvedMessage = interpolateExpression(new TokenIterator( getParameterTokens( resolvedMessage) ), context, locale);
        }
        // last but not least we have to take care of escaped literals
        resolvedMessage = replaceEscapedLiterals( resolvedMessage );
        return resolvedMessage;
    }

    private List<Token> getParameterTokens(String resolvedMessage) {
            return new TokenCollector( resolvedMessage, InterpolationTermType.EL).getTokenList();
    }

    private String resolveMessage(String message, Locale locale) {
        return messageSource.resolveMessage(message,locale);
    }

    private String replaceEscapedLiterals(String resolvedMessage) {
        if ( resolvedMessage.indexOf( '\\' ) > -1 ) {
            resolvedMessage = LEFT_BRACE.matcher( resolvedMessage ).replaceAll( "{" );
            resolvedMessage = RIGHT_BRACE.matcher( resolvedMessage ).replaceAll( "}" );
            resolvedMessage = SLASH.matcher( resolvedMessage ).replaceAll( Matcher.quoteReplacement( "\\" ) );
            resolvedMessage = DOLLAR.matcher( resolvedMessage ).replaceAll( Matcher.quoteReplacement( "$" ) );
        }
        return resolvedMessage;
    }

    private String interpolateExpression(TokenIterator tokenIterator, Context context, Locale locale) {
        while ( tokenIterator.hasMoreInterpolationTerms() ) {
            String term = tokenIterator.nextInterpolationTerm();
            String resolvedExpression = interpolate( context, locale, term );
            tokenIterator.replaceCurrentInterpolationTerm( resolvedExpression );
        }
        return tokenIterator.getInterpolatedMessage();
    }

    public String interpolate(Context context, Locale locale, String term) {
        if ( InterpolationTerm.isElExpression( term ) ) {
            ElTermResolver elTermResolver = new ElTermResolver(locale, ExpressionFactory.newInstance());
            return elTermResolver.interpolate(context,term);
        }
        else {
            ParameterTermResolver parameterTermResolver = new ParameterTermResolver();
            return parameterTermResolver.interpolate( context, term );
        }
    }

}