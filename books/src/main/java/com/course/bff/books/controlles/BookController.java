package com.course.bff.books.controlles;

import com.course.bff.books.models.Book;
import com.course.bff.books.requests.CreateBookCommand;
import com.course.bff.books.responses.BookResponse;
import com.course.bff.books.services.BookService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/v1/books")
public class BookController {

    private final static Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private  BookController bookController;

    @Value("${redis.topic}")
    private String redisTopic;

    public BookController(BookService bookService, RedisTemplate<String, Object> redisTemplate) {
        this.bookService = bookService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping()
    public Collection<BookResponse> getBooks() {
        logger.info("Get book list");
        List<BookResponse> bookResponses = new ArrayList<>();
        this.bookService.getBooks().forEach(book -> {
            BookResponse bookResponse = createBookResponse(book);
            bookResponses.add(bookResponse);
        });

        return bookResponses;
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable UUID id) {
        logger.info(String.format("Find book by id %s", id));
        Optional<Book> bookSearch = this.bookService.findById(id);
        if (bookSearch.isEmpty()) {
            throw new RuntimeException("Book isn't found");
        }


        return createBookResponse(bookSearch.get());
    }

    @PostMapping()
    public BookResponse createBooks(@RequestBody CreateBookCommand createBookCommand) {
        logger.info("Create books");
        Book book = this.bookService.create(createBookCommand);
        BookResponse authorResponse = createBookResponse(book);
        bookController.sendPushNotification(authorResponse);
        return authorResponse;
    }

    @NewSpan("Push notification during book creation")
    public void sendPushNotification(BookResponse bookResponse) {
        logger.info("Push notification during book creation");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            redisTemplate.convertAndSend(redisTopic, gson.toJson(bookResponse));
        } catch (Exception e) {
            logger.error("Push Notification Error", e);
        }
    }

    private BookResponse createBookResponse(Book book) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(book.getId());
        bookResponse.setAuthorId(book.getAuthorId());
        bookResponse.setPages(book.getPages());
        bookResponse.setTitle(book.getTitle());
        return bookResponse;
    }
}
