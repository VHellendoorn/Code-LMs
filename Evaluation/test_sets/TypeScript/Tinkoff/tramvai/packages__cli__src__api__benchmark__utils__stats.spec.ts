import { getSamplesStats } from './stats';

describe('getSamplesStats', () => {
  it('base case', () => {
    const samples = [1, 1, 1, 1, 1];

    expect(getSamplesStats(samples)).toEqual({
      samples,
      mean: 1,
      std: 0,
      variance: 0,
    });
  });

  it('case 1', () => {
    const samples = [1, 2, 3, 4, 5, 6, 7];

    expect(getSamplesStats(samples)).toEqual({
      samples,
      mean: 4,
      std: 2,
      variance: 50,
    });
  });

  it('case 2', () => {
    const samples = [100, 105, 103, 95, 102, 109, 98];

    expect(getSamplesStats(samples)).toEqual({
      samples,
      mean: 101.71428571428571,
      std: 4.266624149448022,
      variance: 4.194714753670809,
    });
  });
});
