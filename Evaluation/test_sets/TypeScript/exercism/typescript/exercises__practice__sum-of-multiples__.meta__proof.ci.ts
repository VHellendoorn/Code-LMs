export const sum = (multiples: number[], limit: number): number => {
  let result = 0
  for (let i = 1; i < limit; i++) {
    if (multiples.some((multiple) => i % multiple === 0)) {
      result += i
    }
  }
  return result
}
