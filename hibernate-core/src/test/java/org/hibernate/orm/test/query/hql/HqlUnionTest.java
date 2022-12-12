package org.hibernate.orm.test.query.hql;

import java.util.List;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DomainModel(
		annotatedClasses = {
				HqlUnionTest.Person.class,
				HqlUnionTest.Dog.class,
		}
)
@SessionFactory
@TestForIssue(jiraKey = "HHH-15766")
public class HqlUnionTest {

	@BeforeAll
	public void setUp(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					Person p = new Person( 1L, "Fabio" );
					Person p2 = new Person( 2L, "Luisa" );
					Dog d = new Dog( 1L, "Pluto" );

					session.persist( p );
					session.persist( p2 );
					session.persist( d );
				}
		);
	}

	@Test
	public void testUnionQueryWithOrderBy(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					List<String> results = session.createQuery(
							"(select p.name as name from Person p order by p.id) union all (select d.name as name from Dog d order by d.id) ",
							String.class
					).list();
					assertThat( results.size() ).isEqualTo( 3 );
				}
		);
	}

	@Test
	public void testUnionQueryWithOrderBy2(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					List<String> results = session.createQuery(
							"select p.name as name from Person p order by p.id union all select d.name as name from Dog d order by d.id ",
							String.class
					).list();
					assertThat( results.size() ).isEqualTo( 3 );
				}
		);
	}

	@Test
	public void testUnionQuery(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					List<String> results = session.createQuery(
							"select p.name as name from Person p union all (select d.name as name from Dog d order by d.id) ",
							String.class
					).list();
					assertThat( results.size() ).isEqualTo( 3 );
				}
		);
	}

	@Test
	public void testUnionQuery2(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					List<String> results = session.createQuery(
							"select p.name as name from Person p union all select d.name as name from Dog d ",
							String.class
					).list();
					assertThat( results.size() ).isEqualTo( 3 );
				}
		);
	}


	@Entity(name = "Person")
	public static class Person {
		@Id
		private Long id;

		private String name;

		public Person() {
		}

		public Person(Long id, String name) {
			this.id = id;
			this.name = name;
		}
	}

	@Entity(name = "Dog")
	public static class Dog {
		@Id
		private Long id;

		private String name;

		public Dog() {
		}

		public Dog(Long id, String name) {
			this.id = id;
			this.name = name;
		}
	}
}
