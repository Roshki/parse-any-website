package com.website_parser.parser.repository;

import com.website_parser.parser.model.Product;
import com.website_parser.parser.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
}
