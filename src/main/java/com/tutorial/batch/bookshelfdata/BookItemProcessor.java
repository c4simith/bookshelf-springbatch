package com.tutorial.batch.bookshelfdata;

import org.springframework.batch.item.ItemProcessor;

public class BookItemProcessor implements ItemProcessor<Book, Book>{

	@Override
    public Book process(final Book book) {
        return book;
    }
}
