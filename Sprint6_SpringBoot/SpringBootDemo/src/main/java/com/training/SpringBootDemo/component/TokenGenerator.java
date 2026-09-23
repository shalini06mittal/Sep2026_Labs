package com.training.SpringBootDemo.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TokenGenerator{

    private int origin;
    private int bound;
//    public TokenGenerator (){
//        System.out.println("Token default constructor");
//    }

    public TokenGenerator(@Value("1") int origin,
                          @Value("10") int bound) {
        this.origin = origin;
        this.bound = bound;
    }

    public int getOrigin() {
        return origin;
    }

    @Value("${origin: 0}")
    public void setOrigin(int origin) {
        this.origin = origin;
    }

    @Value("${bound: 100}")
    public void setBound(int bound) {
        this.bound = bound;
    }

    public int getBound() {
        return bound;
    }
    public int generateToken(){
        return new Random().nextInt(origin,bound);
    }

}

