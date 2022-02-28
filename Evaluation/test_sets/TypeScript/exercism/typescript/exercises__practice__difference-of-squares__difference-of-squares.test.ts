import { Squares } from './difference-of-squares'

describe('Squares', () => {
  describe('up to 5', () => {
    const squares = new Squares(5)

    it('gets the square of sum', () => {
      expect(squares.squareOfSum).toBe(225)
    })

    xit('gets the sum of squares', () => {
      expect(squares.sumOfSquares).toBe(55)
    })

    xit('gets the difference', () => {
      expect(squares.difference).toBe(170)
    })
  })

  describe('up to 10', () => {
    const squares = new Squares(10)

    xit('gets the square of sum', () => {
      expect(squares.squareOfSum).toBe(3025)
    })

    xit('gets the sum of squares', () => {
      expect(squares.sumOfSquares).toBe(385)
    })

    xit('gets the difference', () => {
      expect(squares.difference).toBe(2640)
    })
  })

  describe('up to 100', () => {
    const squares = new Squares(100)

    xit('gets the square of sum', () => {
      expect(squares.squareOfSum).toBe(25502500)
    })

    xit('gets the sum of squares', () => {
      expect(squares.sumOfSquares).toBe(338350)
    })

    xit('gets the difference', () => {
      expect(squares.difference).toBe(25164150)
    })
  })
})
