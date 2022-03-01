export class InvalidInputError extends Error {
  constructor(message: string) {
    super()
    this.message = message || 'Invalid Input'
  }
}

type Direction = 'north' | 'east' | 'south' | 'west'
type Coordinates = [number, number]

export class Robot {
  get bearing(): Direction {
    throw new Error('Remove this statement and implement this function')
  }

  get coordinates(): Coordinates {
    throw new Error('Remove this statement and implement this function')
  }

  place({}: { x: number; y: number; direction: string }) {
    throw new Error('Remove this statement and implement this function')
  }

  evaluate(instructions: string) {
    throw new Error('Remove this statement and implement this function')
  }
}
