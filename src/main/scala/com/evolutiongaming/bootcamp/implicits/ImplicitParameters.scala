package com.evolutiongaming.bootcamp.implicits

object ImplicitParameters {

  // *Implicit parameters ("using" and "given" clauses in Scala 3)*
  //
  // What is the hardest job a typical developer does every day? From my point
  // of view, the hardest job is to do a cognition, to understand the algorithm
  // or business processe encoded in the application and to write a new algorithm,
  // especially if the process or algorithm is not trivial.
  //
  // Implicit parameters are a very powerful approach to drastically decrease
  // the cognitive load and improve the readability of the code without
  // introducing a more advanced Functional Programming techniques.
  //
  // Let's write some simple functions to get a basic understanding about the concept.

  object EvolutionUtils0 {

    /** Provides an actionable context for specific wallet.
      *
      * It allows typical CRUD operations on the wallet. Let's ignore the
      * transactional details, i.e. it is fine to call `read` an then `update`
      * the wallet.
      */
    trait WalletContext {
      def create: Unit
      def read: Option[BigDecimal]
      def update(amount: BigDecimal): Unit
      def delete: Unit
    }

    // Excercise 1:
    // - Implement `CreditService` and `DebitService` in terms of operations on `WalletContext`.
    // - Implement `AwardService` in terms of `CreditService` and `DebitService` calls.
    class CreditService {

      /** Gives money to wallet, creates a wallet if does not exist yet */
      def credit(context: WalletContext, amount: BigDecimal): Unit = ???
    }
    class DebitService {

      /** Removes money from wallet */
      def debit(context: WalletContext, amount: BigDecimal): Unit = ???
    }
    class TransferService(creditService: CreditService, debitService: DebitService) {

      /** Either does credit or debit depending on the amount */
      def transfer(context: WalletContext, amount: BigDecimal): Unit = ???
    }

    // This is the way it could be called:
    trait WalletRepository {
      def getWallet(userId: String): WalletContext
    }
    class WalletController(walletRepository: WalletRepository, transferService: TransferService) {
      def bet(userId: String, amount: BigDecimal): Unit   = {
        val walletContext = walletRepository.getWallet(userId)
        transferService.transfer(walletContext, -amount)
      }
      def award(userId: String, amount: BigDecimal): Unit = {
        val walletContext = walletRepository.getWallet(userId)
        transferService.transfer(walletContext, amount)
      }
    }

    // Do you like the code you just written? What did you feel while writing it?
    // Was it convenient enough? Did you feel something bothering you?

  }

  // One of the issues we encoutered is an additional and quite useless
  // cognitive load created by having to differentiate between the normal
  // parameters and the context. We do not really care about `context`
  // parameter, we just want to pass it over to the next function.
  //
  // What if we had the way to group parameters and highlight the fact that
  // context should be just passed over.
  //
  // One way we could use to improve our life is to actually use mulitple
  // parameter lists to, indeed, group parameters and separate useful parameters
  // from these that should be just passed over.
  //
  // I.e. you can write `method(a, b)(c, d)` instead of `method(a, b, c, d)`
  // and then call it using the same arguments.
  //
  // See also: https://docs.scala-lang.org/tour/multiple-parameter-lists.html
  //
  object EvolutionUtils1 {

    // Let's write the same code using multiple parameters lists

    trait WalletContext {
      def create: Unit
      def read: Option[BigDecimal]
      def update(amount: BigDecimal): Unit
      def delete: Unit
    }

    class CreditService {
      def credit(amount: BigDecimal)(context: WalletContext): Unit = ???
    }
    class DebitService {
      def debit(amount: BigDecimal)(context: WalletContext): Unit = ???
    }
    class TransferService(creditService: CreditService, debitService: DebitService) {
      def transfer(amount: BigDecimal)(context: WalletContext): Unit = ???
    }

    // This is the way it could be called:
    trait WalletRepository {
      def getWallet(userId: String): WalletContext
    }
    class WalletController(walletRepository: WalletRepository, transferService: TransferService) {
      def bet(userId: String, amount: BigDecimal): Unit   = {
        val walletContext = walletRepository.getWallet(userId)
        transferService.transfer(-amount)(walletContext)
      }
      def award(userId: String, amount: BigDecimal): Unit = {
        val walletContext = walletRepository.getWallet(userId)
        transferService.transfer(amount)(walletContext)
      }
    }

    // Do you like the new approach? Does it look more verbose or less verbose?
    // Would you use it in your application?

  }

  // While some could say the new approach with multiple parameter lists is an
  // improvement, I would insist the code still looks quite verbose and throws
  // `walletContext` into our eyes increasing a cognitive load and making the
  // code both harder to read and original business process harder to undertand.
  //
  // We still see a lot of `walletContext` in our `TransferService` despite not
  // caring about it at all. What if we could have a technique which would allow
  // us to avoid passing `walletContext` over and over, but still having access
  // to it?
  //
  // Turns out there is such a technique in Scala! It is called implicit
  // parameters.
  //
  // Turns out you can add `implicit` keyword in last block of parameters in
  // multiple parameter list and it will be passed forward automatically.
  //
  object EvolutionUtils2 {

    // Exercise 2:
    // - Add `implicit` keyword into each `(context: WalletContext)` block,
    //   i.e. make it look like `(implicit context: WalletContext)` block.
    // - Ensure the code compiles before going to a next step.
    // - Remove explicit calls in your `transfer` method by calling
    //   `credit(amount)` and `debit(amount)` without specifying the second
    //   parameter.

    trait WalletContext {
      def create: Unit
      def read: Option[BigDecimal]
      def update(amount: BigDecimal): Unit
      def delete: Unit
    }

    class CreditService {
      def credit(amount: BigDecimal)(context: WalletContext): Unit = ???
    }
    class DebitService {
      def debit(amount: BigDecimal)(context: WalletContext): Unit = ???
    }
    class TransferService(creditService: CreditService, debitService: DebitService) {
      def transfer(amount: BigDecimal)(context: WalletContext): Unit = ???
    }

    // This is the way it could be called:
    trait WalletRepository {
      def getWallet(userId: String): WalletContext
    }
    class WalletController(walletRepository: WalletRepository, transferService: TransferService) {
      def bet(userId: String, amount: BigDecimal): Unit   = {
        val walletContext = walletRepository.getWallet(userId)
        transferService.transfer(-amount)(walletContext)
      }
      def award(userId: String, amount: BigDecimal): Unit = {
        val walletContext = walletRepository.getWallet(userId)
        transferService.transfer(amount)(walletContext)
      }
    }

    // Do you like the new approach? Does it look more verbose or less verbose?
    // Would you use it in your application?

  }

  // Now watch this video:
  // https://www.youtube.com/watch?v=Lm4LYX3xdkU
  //
  // Yes! You learned implicit parameters and together with implicit classes you
  // also learned 100% of implicits. It is _that_ easy. Everything else following
  // is advanced patterns of using these two techniques separately or together,
  // and some syntax sugar arround these techniques to make certain patterns
  // easier to use.
  //
  // Note: I will tell you about advanced pattern shortly, no need to demand
  // your money back.

}
