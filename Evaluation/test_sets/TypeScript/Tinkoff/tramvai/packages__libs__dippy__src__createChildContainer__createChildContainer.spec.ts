import { createChildContainer } from './createChildContainer';
import { createContainer } from '../createContainer/createContainer';
import { createToken } from '../createToken/createToken';

describe('createContainer', () => {
  it('проверка инициализации', () => {
    const token = createToken('logger');
    const container = createContainer();
    container.register({ provide: token, useValue: 'log' });
    const childContainer = createChildContainer(container);
    childContainer.register({ provide: 'config', useValue: 'conffig' });

    expect(childContainer.get(token)).toBe('log');
    expect(childContainer.get('config')).toBe('conffig');
  });
});
