import identity from '@tinkoff/utils/function/identity';

const mockFunc = jest.fn(identity);

jest.mock('./wrapMapToProps', () => ({
  wrapMapToPropsFunc: mockFunc,
  wrapMapToPropsConstant: mockFunc,
}));

describe('connect/mapStateToProps', () => {
  beforeEach(() => {
    mockFunc.mockClear();
  });

  it('default export is array', () => {
    expect(jest.requireActual('./mapStateToProps').mapStateToPropsFactories).toHaveLength(2);
  });

  describe('whenMapStateToPropsIsFunction', () => {
    const tst = jest.requireActual('./mapStateToProps').whenMapStateToPropsIsFunction;

    it('should calls wrap if mapState is function', () => {
      const map = (obj) => ({ ...obj, a: 1 });
      const res = tst(map);

      expect(res).toBe(map);
      expect(mockFunc).toHaveBeenCalledWith(map, 'mapStateToProps');
      expect(res({ b: 2 })).toEqual({ a: 1, b: 2 });
    });

    it('should return undefined otherwise', () => {
      expect(tst()).toBeUndefined();
      expect(tst({})).toBeUndefined();
      expect(tst(/fawfwf/)).toBeUndefined();
      expect(tst('fawffffg')).toBeUndefined();
      expect(mockFunc).not.toHaveBeenCalled();
    });
  });

  describe('whenMapStateToPropsIsMissing', () => {
    const tst = jest.requireActual('./mapStateToProps').whenMapStateToPropsIsMissing;

    it('should calls wrap with identity function if mapState is falsy', () => {
      const map = tst();

      expect(map).toBeInstanceOf(Function);
      expect(map()).toEqual({});
    });

    it('should return undefined otherwise', () => {
      expect(tst(() => {})).toBeUndefined();
      expect(tst({})).toBeUndefined();
      expect(tst(/fawfwf/)).toBeUndefined();
      expect(tst('fawffffg')).toBeUndefined();
      expect(mockFunc).not.toHaveBeenCalled();
    });
  });
});
