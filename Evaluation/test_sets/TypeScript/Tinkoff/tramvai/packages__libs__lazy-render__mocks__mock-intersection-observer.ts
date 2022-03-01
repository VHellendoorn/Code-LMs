const mockIntersectionObserver = {
  callback: null,
  observe: jest.fn(),
  unobserve: jest.fn(),
  disconnect: jest.fn(() => {
    mockIntersectionObserver.clear();
  }),
  trigger: (entries) => {
    if (mockIntersectionObserver.callback) {
      mockIntersectionObserver.callback(entries, mockIntersectionObserver);
    }
  },
  clear: () => {
    mockIntersectionObserver.callback = null;
    mockIntersectionObserver.observe.mockClear();
    mockIntersectionObserver.disconnect.mockClear();
  },
};

Object.defineProperty(window, 'IntersectionObserver', {
  writable: true,
  value: jest.fn().mockImplementation((callback) => {
    mockIntersectionObserver.callback = callback;
    return mockIntersectionObserver;
  }),
});

export { mockIntersectionObserver };
