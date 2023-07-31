package ru.udya.rdatamanager;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import ru.udya.rdatamanager.entity.Person;
import ru.udya.rdatamanager.store.JpaRDataStore;

import java.util.stream.IntStream;

@SpringBootTest
class RdmTest {

	@Autowired
	Metadata metadata;

	@Autowired
	DataManager dataManager;

	@Autowired
	UnconstrainedDataManager unconstrainedDataManager;

	@Autowired
	JpaRDataStore jpaRDataStore;

//	@Test
//	void contextLoads() {
//
//		var person = dataManager.create(Person.class);
//		person.setFirstName("John");
//		person.setLastName("Doe");
//
//		dataManager.save(person);
//
//		System.out.println("contextLoads " + Thread.currentThread());
//
//		var lc = new LoadContext<>(metadata.getClass(Person.class));
//		lc.setId(person.getId());
//		var mono = jpaRDataStore.load(lc);
//		var mono2 = jpaRDataStore.load(lc);
//
//		System.out.println("after start loading " + Thread.currentThread());
//
//		mono.thenAccept(e -> System.out.println("mono before get " + Thread.currentThread()));
//
//		var voidCompletableFuture = CompletableFuture.allOf(mono, mono2);
//		try {
//			voidCompletableFuture.get();
//		} catch (InterruptedException | ExecutionException e) {
//			throw new RuntimeException(e);
//		}
//
//
//		mono2.thenAccept(e -> System.out.println("mono2 after get " + Thread.currentThread()));
//		mono.thenAccept(e -> System.out.println("mono after get " + Thread.currentThread()));
//	}

	@Test
	void contextLoadsMono() {

		System.out.println("Start creating");

		var creatingStart = System.currentTimeMillis();

		var persons = IntStream.range(0, 1000).mapToObj(i -> {
			var person = dataManager.create(Person.class);
			person.setFirstName("John");
			person.setLastName("Doe");
			return person;
		}).toList();

		var saveContext = new SaveContext();
		saveContext.getEntitiesToSave().addAll(persons);

		dataManager.save(saveContext);


		System.out.println("Creating spent time: " + (System.currentTimeMillis() - creatingStart));

		for (int i = 0; i <100; i++) {

			var loadingStart = System.currentTimeMillis();

			var monos = persons.stream().map(p -> {
				var lc = new LoadContext<>(metadata.getClass(Person.class));
				lc.setId(p.getId());
				return jpaRDataStore.load(lc);
			}).toList();

			// waiting result
			Flux.merge(monos).then().block();

			System.out.println("Loading spent time: " + (System.currentTimeMillis() - loadingStart));
		}
	}
}
