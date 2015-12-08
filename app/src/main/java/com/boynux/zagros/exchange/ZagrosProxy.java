package com.boynux.zagros.exchange;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.boynux.zagros.exchange.Models.ExchangeRate;
import com.boynux.zagros.exchange.Models.HealthInfo;

/**
 * Created by mamad on 10/30/15.
 */
public interface ZagrosProxy {
    @LambdaFunction
    HealthInfo zagrosHelthCheck(String fix);

    @LambdaFunction
    ExchangeRate[] zagrosExchangeRates(String fix);

    @LambdaFunction
    String zagrosCopyrightContent(String fix);

    @LambdaFunction
    String zagrosDisclaimerContent(String fix);
}