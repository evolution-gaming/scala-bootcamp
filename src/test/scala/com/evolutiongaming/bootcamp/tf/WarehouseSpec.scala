package com.evolutiongaming.bootcamp.tf

import cats.Id
import org.scalatest.flatspec.AnyFlatSpec

final class WarehouseSpec extends AnyFlatSpec {

  "Warehouse logic" must "create items" in {
    val result = Warehouse.program[Id](new TestConsole("Test", 123), new TestItems())
    assert(result == Right(Item(0, "Test", 123)))
  }

  it must "return error on empty names" in {
    val result = Warehouse.program[Id](new TestConsole("", 123), new TestItems())
    assert(result == Left(Items.ValidationError.EmptyName))
  }

  it must "return error on negative prices" in {
    val result = Warehouse.program[Id](new TestConsole("Test", -1), new TestItems())
    assert(result == Left(Items.ValidationError.NegativePrice))
  }

  private final class TestConsole(name: String, price: BigDecimal) extends Console[Id] {
    override def readStr: Id[String] = name
    override def readBigDecimal: Id[BigDecimal] = price
    override def putStrLn(str: String): Id[Unit] = ()
  }

  private final class TestItems extends Items[Id] {
    private var counter = 0L
    private var items   = Map.empty[Long, Item]

    override def all: Id[Map[Long, Item]] = items

    override def create(name: String, price: BigDecimal): Id[Either[Items.ValidationError, Item]] =
      Items.validate(name, price).map { case (name, price) =>
        val item = Item(counter, name, price)
        items += counter -> item
        counter += 1
        item
      }

    override def update(item: Item): Id[Either[Items.ValidationError, Boolean]] =
      Items.validate(item.name, item.price).map { _ =>
        if (items.contains(item.id)) {
          items += item.id -> item
          true
        } else {
          false
        }
      }

    override def find(id: Long): Id[Option[Item]] = items.get(id)

    override def delete(id: Long): Id[Boolean] =
      if (items.contains(id)) {
        items -= id
        true
      } else {
        false
      }
  }
}
