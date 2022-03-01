export function find(array: number[], element: number): number | never {
  let start = 0
  let end = array.length - 1
  let middle: number

  while (start <= end) {
    middle = Math.floor((start + end) / 2)
    if (element === array[middle]) {
      return middle
    } else if (element < array[middle]) {
      end = middle - 1
    } else if (element > array[middle]) {
      start = middle + 1
    }
  }

  throw new Error('Value not in array')
}
