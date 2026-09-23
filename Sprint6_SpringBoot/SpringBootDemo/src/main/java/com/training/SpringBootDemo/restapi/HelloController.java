package com.training.SpringBootDemo.restapi;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/greet")
public class HelloController {

    public HelloController(){
        System.out.println("Hello controller constructor");
    }
    @GetMapping
    public String greet1(){
        return " GET Greetings";
    }
    @PostMapping
    public String greet2(){
        return " GET Greetings";
    }
    //curl -X 'PUT' \
    //  'http://localhost:8081/greet' \
    //  -H 'accept: */*'
    @PutMapping
    public String greet3(){
        return " GET Greetings";
    }
    @DeleteMapping
    public String greet4(){
        return " GET Greetings";
    }
    @GetMapping("/{id}")
    public String greet5(@PathVariable int id){
        return " GET Greetings "+id;
    }

    @GetMapping("/price")
    public String price(@RequestParam(defaultValue = "10") Integer qty) {
        return "qty=" + qty;
    }

    @RequestMapping("/ping")
    public String ping() { return "pong"; }
}
