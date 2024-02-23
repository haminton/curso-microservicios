package com.haminton.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.haminton.customer.entities.CustomerEntity;
import com.haminton.customer.entities.CustomerProduct;
import com.haminton.customer.entities.CustomerTransaction;
import com.haminton.customer.repository.CustomerRepositoy;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    CustomerRepositoy customerRepository;

    private final WebClient.Builder webClientBuilder;

    public CustomerController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }


    //webClient requires HttpClient library to work propertly
    HttpClient client = HttpClient.create()
            //Connection Timeout: is a period within which a connection between a client and a server must be established
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(EpollChannelOption.TCP_KEEPIDLE, 300)
            .option(EpollChannelOption.TCP_KEEPINTVL, 60)
            //Response Timeout: The maximun time we wait to receive a response after sending a request
            .responseTimeout(Duration.ofSeconds(1))
            // Read and Write Timeout: A read timeout occurs when no data was read within a certain
            //period of time, while the write timeout when a write operation cannot finish at a specific time
            .doOnConnected(connection -> {
                connection.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                connection.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    @GetMapping()
    public List<CustomerEntity> findAll() {
        return customerRepository.findAll();
    }

    @GetMapping("/{id}")
    public CustomerEntity findById(@PathVariable Long id) {
        return customerRepository.findById(id).get();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable Long id, @RequestBody CustomerEntity request) {
        CustomerEntity customer = customerRepository.findById(id).get();
        if (Objects.nonNull(customer)) {
            customer.setCode(request.getCode());
            customer.setNames(request.getNames());
            customer.setIban(request.getIban());
            customer.setPhone(request.getPhone());
            customer.setAddress(request.getAddress());
            customer.setSurname(request.getSurname());
        }
        CustomerEntity save = customerRepository.save(customer);
        return ResponseEntity.ok(save);
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody CustomerEntity request) {
        request.getProducts().forEach(element -> element.setCustomer(request));
        CustomerEntity save = customerRepository.save(request);
        return ResponseEntity.ok(save);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Optional<CustomerEntity> customer = customerRepository.findById(id);
        if (Objects.nonNull(customer.get())) {
            customerRepository.delete(customer.get());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/full")
    public CustomerEntity getByCode(@RequestParam String code) {
        CustomerEntity customer = customerRepository.findByCode(code);
        List<CustomerProduct> products = customer.getProducts();
        List<CustomerTransaction> transactions =  getTransactionsCustomer(customer.getIban());
        customer.setTransactions(transactions);
        products.forEach(x ->{
            String productName = getProductName(x.getProductId());
            x.setProductName(productName);
        });
        return customer;

    }




    private String getProductName(long id) {
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8086/product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8086/product"))
                .build();
        JsonNode block = build.method(HttpMethod.GET).uri("/" + id)
                .retrieve().bodyToMono(JsonNode.class).block();
        String name = block.get("name").asText();
        return name;
    }

    public List<CustomerTransaction> getTransactionsCustomer(String accountIban) {
        String url = "http://localhost:8087/transaction";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            Map<String, Object> response = webClientBuilder
                    .baseUrl(url)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", url))
                    .build()
                    .method(HttpMethod.GET)
                    .uri("/" + accountIban)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<CustomerTransaction> customerTransactions = new ArrayList<>();
            if (response != null) {
                List<Map<String, Object>> transactions = (List<Map<String, Object>>) response.get("transactions");
                transactions.forEach(element -> {
                    customerTransactions.add(CustomerTransaction.builder()
                            .id(Long.parseLong(element.get("id").toString()))
                            .fee(Double.parseDouble(element.get("fee").toString()))
                            .date(LocalDate.parse(element.get("date").toString(), formatter))
                            .accountIban(element.get("accountIban").toString())
                            .amount(Double.parseDouble(element.get("amount").toString()))
                            .description(element.get("description").toString())
                            .reference(element.get("reference").toString())
                            .status(element.get("status").toString())
                            .build()
                    );
                });

            }
            return  customerTransactions;

        } catch (WebClientResponseException e) {
            System.out.printf("exepcion " + e);
            return Collections.emptyList();
        }
    }



}
