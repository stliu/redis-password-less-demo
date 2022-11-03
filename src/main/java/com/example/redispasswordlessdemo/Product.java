package com.example.redispasswordlessdemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("product")
public class Product implements Serializable {
    private String id;
    private String name;
    private double price;

}
