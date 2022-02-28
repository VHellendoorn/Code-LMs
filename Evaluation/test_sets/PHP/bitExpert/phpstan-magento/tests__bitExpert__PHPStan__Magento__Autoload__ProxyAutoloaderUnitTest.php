<?php

/*
 * This file is part of the phpstan-magento package.
 *
 * (c) bitExpert AG
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
declare(strict_types=1);

namespace bitExpert\PHPStan\Magento\Autoload;

use PHPStan\Cache\Cache;
use PHPStan\Cache\CacheStorage;
use PHPUnit\Framework\TestCase;

class ProxyAutoloaderUnitTest extends TestCase
{
    /**
     * @var CacheStorage|\PHPUnit\Framework\MockObject\MockObject
     */
    private $storage;
    /**
     * @var ProxyAutoloader
     */
    private $autoloader;

    public function setUp(): void
    {
        $this->storage = $this->createMock(CacheStorage::class);
        $this->autoloader = new ProxyAutoloader(new Cache($this->storage));
    }

    /**
     * @test
     */
    public function autoloaderIgnoresClassesWithoutProxyPostfix(): void
    {
        $this->storage->expects(self::never())
            ->method('load');

        $this->autoloader->autoload('SomeClass');
    }

    /**
     * @test
     */
    public function autoloaderUsesCachedFileWhenFound(): void
    {
        $this->storage->expects(self::once())
            ->method('load')
            ->willReturn(__DIR__ . '/HelperProxy.php');

        $this->autoloader->autoload('\bitExpert\PHPStan\Magento\Autoload\Helper\Proxy');

        self::assertTrue(class_exists(HelperProxy::class, false));
    }

    /**
     * @test
     */
    public function autoloaderGeneratesCacheFileWhenNotFoundInCache(): void
    {
        // little hack: the proxy autoloader will use Reflection to look for a class without the \Proxy prefix,
        // to avoid having another stub class file, we define an class alias here
        class_alias('\bitExpert\PHPStan\Magento\Autoload\HelperProxy', '\bitExpert\PHPStan\Magento\Autoload\Helper');

        $this->storage->expects(self::atMost(2))
            ->method('load')
            ->willReturnOnConsecutiveCalls(null, __DIR__ . '/HelperProxy.php');
        $this->storage->expects(self::once())
            ->method('save');

        $this->autoloader->autoload('\bitExpert\PHPStan\Magento\Autoload\Helper\Proxy');

        self::assertTrue(class_exists(HelperProxy::class, false));
    }
}
