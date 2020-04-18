/*
 * MIT License
 *
 * Copyright (c) 2020 David Greven
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package grevend.persistencelite.main;

import grevend.persistencelite.entity.Entity;
import grevend.persistencelite.entity.Id;
import grevend.persistencelite.entity.Ignore;
import grevend.persistencelite.entity.Property;
import grevend.persistencelite.service.sql.PostgresService;
import java.util.Date;
import java.util.Map;
import org.jetbrains.annotations.Contract;

public class Main {

    public static void main(String[] args) throws Throwable {
        /*System.out.println("Hello World!");
        System.out.println(EntityMetadata.of(Test.class));*/
        //System.out.println(EntityMetadata.of(Test.class).getConstructor());
        //System.out.println(LookupUtils.lookupInterfaceEntityAttributes(EntityMetadata.of(Test2.class)));
        //System.out.println(LookupUtils.lookupRecordEntityAttributes(EntityMetadata.of(Test3.class)));
        /*var entity = EntityFactory.construct(EntityMetadata.of(Test.class), Map.of(
            "a", 12,
            "b", 21,
            "c", "Hi?",
            "d", Date.from(Instant.now()),
            "helloWorld", "Hello World!"
        ));*/
        /*var entity = EntityFactory.construct(EntityMetadata.of(Customer.class), Map.of(
            "id", 12,
            "username", "Bob",
            "password", "123456789",
            "email", "bob@van.com",
            "companyName", "Van...",
            "accountId", 12
        ));
        System.out.println(entity);
        System.out.println(EntityFactory.deconstruct(EntityMetadata.of(Customer.class), entity));*/

        PostgresService service = new PostgresService();
        var dao = service.createDao(Customer.class);
        var customer = new Customer(21, "Justin", "987654321", "justin@van.com", "Van...", 21);
        dao.create(new Customer(12, "Bob", "123456789", "bob@van.com", "Van...", 12));
        dao.create(customer);
        System.out.println(dao.retrieve());
        System.out.println(dao.retrieve(Map.of("id", 12, "username", "Bob")));
        System.out.println(dao.retrieve(Map.of("id", 13, "username", "Bob")));
        dao.delete(customer);
        System.out.println(dao.retrieve());
        //System.out.println(EntityMetadata.of(Customer.class).toStructuredString());
    }

    @Entity(name = "test2")
    private interface Test2 {

        @Contract(pure = true)
        static int test() {
            return 0;
        }

        int a();

        @Property(name = "abcde")
        int b();

        String c();

        Date d();

        @Property(name = "helloWorld", autoGenerated = true)
        String helloWorld();

        @Ignore
        String i();

        default int g() {
            return 0;
        }

    }

    @Entity(name = "account_base")
    public interface AccountBase {

        @Id
        int id();

        @Id
        String username();
    }

    @Entity(name = "account2")
    public interface Account extends AccountBase {

        @Id
        int id();

        @Id
        String username();

        String password();
    }

    @Entity(name = "employee")
    public interface Employee extends Account {

        int id();

        @Property(name = "first_name")
        String firstName();

        @Property(name = "middle_name")
        String middleName();

        @Property(name = "last_name")
        String lastName();

        @Property(name = "date_of_birth")
        Date dateOfBirth();

        String bsn();

        @Property(name = "account_id")
        int accountId();

        @Property(name = "address_id")
        int addressId();
    }

    @Entity(name = "customer")
    public record Customer(@Id int id, @Id String username, String password, String email, @Property(name = "company_name")String companyName,
                           @Property(name = "account_id")int accountId) implements Account {}

    @Entity(name = "test")
    public record Test(int a, @Property(name = "abcde")int b, String c, Date d, String helloWorld) {}

    @Entity(name = "test3")
    private static class Test3 {

        public static String l;
        public final String f = "FINAL";
        public int a;
        public String helloWorld;
        @Ignore
        public String t;
        public volatile String v;
        protected String c;
        Date d;
        @Property(name = "abcde")
        private int b;

    }

    record Owner(int id, String username, String password, @Property(name = "first_name")String firstName, @Property(name = "middle_name")String middleName,
                 @Property(name = "last_name")String lastName, @Property(name = "date_of_birth")Date dateOfBirth, String bsn, @Property(name = "account_id")int accountId,
                 @Property(name = "address_id")int addressId, @Property(name = "employee_id")int employeeId) implements Employee {}

}
