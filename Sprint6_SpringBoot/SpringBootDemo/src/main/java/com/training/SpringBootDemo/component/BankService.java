package com.training.SpringBootDemo.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankService {

    private TokenGenerator tokenGenerator;

//    public BankService() {
//        System.out.println("Bank Service default constructor");
//        System.out.println(tokenGenerator);
//    }

//    @Autowired
    public BankService(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
      //  System.out.println(tokenGenerator);
    }

    public TokenGenerator getTokenGenerator() {
        return tokenGenerator;
    }

    public void setTokenGenerator(TokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }
    public int getTokenValue(){

        System.out.println(tokenGenerator.generateToken());
        return tokenGenerator.generateToken();
    }

}
