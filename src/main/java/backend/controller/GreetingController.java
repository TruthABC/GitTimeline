package backend.controller;

import backend.entity.response.GreetingResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    @CrossOrigin
    public GreetingResponse greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new GreetingResponse(counter.incrementAndGet(),
                String.format(template, name));
    }
}