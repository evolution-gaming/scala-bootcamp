//format: off

/**
  * ╔════════════════════════════════════════╗
  * ║    V a r i a n c e  P o s i t i o n s  ║
  * ╚════════════════════════════════════════╝
  */

//format: on

// Default is covariant

// flip the sign when you go inside:

// 1. Parameter
// 2. Type Parameter
// 3. Lower Bound
// 4. Contravariant Type Constructor
//
// usage of type equality or invariant type constructor makes position invariant

trait CheckingVariance[-In, +Out, Stop, `?`] {
  def value: `?`

  def method1(param: `?`): `?`

  type Contra[-A]
  type Co[+A]
  type Inv[A]

  def method2(param: Co[`?`]): Co[`?`]
  def method3(param: Contra[`?`]): Contra[`?`]
  def method4(param: Inv[`?`]): Inv[`?`]
  def method5(param: Co[Contra[`?`]]): Co[Contra[`?`]]
  def method6(param: Contra[Co[`?`]]): Contra[Co[`?`]]
  def method7(param: Contra[Contra[`?`]]): Contra[Contra[`?`]]
  def method8(param: Co[Inv[`?`]]): Co[Inv[`?`]]
  def method9(param: Inv[Co[`?`]]): Inv[Co[`?`]]

  type Upper <: `?`
  type Lower >: `?`
  type EqCo = Co[`?`]

  type UpperCo <: Co[`?`]
  type LowerCo >: Co[`?`]
  type EqCo = Co[`?`]

  type UpperContra <: Contra[`?`]
  type LowerContra >: Contra[`?`]
  type EqContra = Contra[`?`]

  type Inside[x >: `?`, y <: `?`]
  type InsideCo[x >: Co[`?`], y <: Co[`?`]]
  type InsideContra[x >: Contra[`?`], y <: Contra[`?`]]

  type TwoStepInside[f[x <: `?`, y >: `?`]]
  type TwoStepInsideCo[f[x <: Co[`?`], y >: Co[`?`]]]
  type TwoStepInsideContra[f[x <: Contra[`?`], y >: Contra[`?`]]]

}

//format: off
















// format: on

trait CheckingVarianceAnswers[-In, +Out, Stop] {
  def value: Out

  def method1(param: In): Out

  type Contra[-A]
  type Co[+A]
  type Inv[A]

  def method2(param: Co[In]): Co[Out]
  def method3(param: Contra[Out]): Contra[In]
  def method4(param: Inv[Stop]): Inv[Stop]
  def method5(param: Co[Contra[Out]]): Co[Contra[In]]
  def method6(param: Contra[Co[Out]]): Contra[Co[In]]
  def method7(param: Contra[Contra[In]]): Contra[Contra[Out]]
  def method8(param: Co[Inv[Stop]]): Co[Inv[Stop]]
  def method9(param: Inv[Co[Stop]]): Inv[Co[Stop]]

  type Upper <: Out
  type Lower >: In
  type Eq = Stop

  type UpperCo <: Co[Out]
  type LowerCo >: Co[In]
  type EqCo = Co[Stop]

  type UpperContra <: Contra[In]
  type LowerContra >: Contra[Out]
  type EqContra = Contra[Stop]

  type Inside[x >: Out, y <: In]
  type InsideCo[x >: Co[Out], y <: Co[In]]
  type InsideContra[x >: Contra[In], y <: Contra[Out]]

  type TwoStepInside[f[x <: Out, y >: In]]
  type TwoStepInsideCo[f[x <: Co[Out], y >: Co[In]]]
  type TwoStepInsideContra[f[x <: Contra[In], y >: Contra[Out]]]

}
