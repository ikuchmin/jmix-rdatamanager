package ru.udya.rdatamanager.dm;

import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.udya.rdatamanager.entity.Person;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UnconstrainedRDataManagerImplTest {

    @Autowired
    Metadata metadata;

    @Autowired
    UnconstrainedDataManager unconstrainedDataManager;
    
    @Autowired
    UnconstrainedRDataManager unconstrainedRDataManager;

    @Test
    void basicTest() {
        var person = unconstrainedDataManager.create(Person.class);
        person.setLastName("Doe");
        person.setFirstName("John");

        unconstrainedDataManager.save(person);

        var lc = new LoadContext<>(metadata.getClass(Person.class));
        lc.setId(person.getId());

        System.out.println(unconstrainedRDataManager.load(lc)
                .doOnNext(e -> System.out.println(Thread.currentThread())).block());
    }

    @Test
    void basicLoadListTest() {
        var people = IntStream.range(0, 10).mapToObj(i -> {
            var person = unconstrainedDataManager.create(Person.class);
            person.setLastName("Doe");
            person.setFirstName("John");
            return person;
        }).toList();

        unconstrainedDataManager.save(people.toArray());

        var lc = new LoadContext<>(metadata.getClass(Person.class));

        System.out.println(unconstrainedRDataManager.loadList(lc)
                .doOnNext(e -> System.out.println(Thread.currentThread()))
                .toStream().toList());
    }

    @Test
    void basicSaveTest() {
        var people = IntStream.range(0, 10).mapToObj(i -> {
            var person = unconstrainedDataManager.create(Person.class);
            person.setLastName("Doe");
            person.setFirstName("John");
            return person;
        }).toList();

        unconstrainedRDataManager.save(people.toArray());

        var lc = new LoadContext<>(metadata.getClass(Person.class));

        System.out.println(unconstrainedRDataManager.loadList(lc)
                .doOnNext(e -> System.out.println(Thread.currentThread()))
                .toStream().toList());
    }
}