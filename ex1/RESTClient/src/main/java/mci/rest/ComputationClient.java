package mci.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import java.util.Scanner;

public class ComputationClient {

    public static void main(String args[]) {

        // Input Scanner
        Scanner inputScanner = new Scanner(System.in);

        // Create client
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target("http://localhost:8080/api");
        ResteasyWebTarget rtarget = (ResteasyWebTarget)target;

        // Services
        TestService hello = rtarget.proxy(TestService.class);
        ComputationService calc = rtarget.proxy(ComputationService.class);

        // Get hello message from server
        String msg = hello.getHelloMessage();
        System.out.println("Testing Hello World service");
        System.out.println("Received from server: " + msg);

        // Test Compute
        System.out.println("Testing Computation service");
        System.out.println("Please enter the first number: ");
        int n1 = inputScanner.nextInt();
        System.out.println("Please enter the second number: ");
        int n2 = inputScanner.nextInt();
        System.out.println("Please enter the operation (add, sub, mul, div): ");
        String op = inputScanner.next();

        String result = calc.calculate(n1, n2, op);
        System.out.println("Result received from server: " + result);
    }

}
