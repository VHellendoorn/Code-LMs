<?php

namespace VentTest;

/**
 * Tests that don't concern scope live here
 * Class VariableTest
 * @package VentTest
 */
class VariableTest extends VentTestCase
{

    public function testReadRetainingResponse()
    {
        $counter = 0;
        $readAttempts = 5;
        $runOnce = function() use (&$counter){
            $counter++;
            if ($counter > 1)
            {
                throw new \Exception('This callable should have only ever been run once!');
            }
            return 'madeUpRetainableResponse';
        };

        $user = new External\Classes\User();
        $user->registerEvent('read', 'name', $runOnce, null, true);
        for ($x = 0; $x < $readAttempts; $x++)
        {
            $user->name;
        }
        $this->assertEquals(1, $counter);
    }

    public function testReadNonRetainingResponse()
    {
        $counter = 0;
        $readAttempts = 5;
        $runMany = function() use (&$counter){
            $counter++;
            return 'madeUpResponse';
        };

        $user = new External\Classes\User();
        $user->registerEvent('read', 'name', $runMany);
        for ($x = 0; $x < $readAttempts; $x++)
        {
            $user->name;
        }

        $this->assertEquals($readAttempts, $counter);
    }


    public function testWriteRetainingResponse()
    {
        $counter = 0;
        $writeAttempts = 5;
        $runOnce = function() use (&$counter){
            $counter++;
            if ($counter > 1)
            {
                throw new \Exception('This callable should have only ever been run once!');
            }
            return 'madeUpRetainableResponse';
        };

        $user = new External\Classes\User();
        $user->registerEvent('write', 'name', $runOnce, null, true);
        for ($x = 0; $x < $writeAttempts; $x++)
        {
            $user->name = 'test';
        }
        $this->assertEquals(1, $counter);
    }


    public function testWriteNonRetainingResponse()
    {
        $counter = 0;
        $writeAttempts = 5;
        $runMany = function() use (&$counter){
            $counter++;
            return 'madeUpResponse';
        };

        $user = new External\Classes\User();
        $user->registerEvent('write', 'name', $runMany);
        for ($x = 0; $x < $writeAttempts; $x++)
        {
            $user->name = 'test';
        }

        $this->assertEquals($writeAttempts, $counter);
    }

    public function testMasqueradingOnVariableWrite()
    {
        $value = 'test213';
        $user = new External\Classes\User();
        $user->registerEvent('write', 'name', function() use ($value){
            return $value;
        });

        $user->name = 'somethingElse';

        $this->assertEquals($value, $user->name);
    }

    public function testObjectReadEvent()
    {
        $user = new External\Classes\User();
        $user->name = new \StdClass();
        $firstName = 'LeeRoy';
        $user->name->first = $firstName;

        $counter = 0;
        $user->registerEvent('read', 'name', function() use (&$counter){
            $counter++;
        });

        $this->assertSame($firstName, $user->name->first);
        $this->assertEquals(1, $counter);
    }

    /**
     * @expectedException \Exception
     */
    public function testDeleteEvent()
    {
        $user = new External\Classes\User();
        $user->registerEvent('delete', 'name', function() {
            throw new \Exception('delete event run');
        });
        unset($user->name);
    }

    /**
     * Note that any writes to a property of an object WONT trigger the event (that'll need to registered in the scope of that object)
     * So $user->name->random = 'something' (name being an object) will NOT trigger an event
     */
    public function testObjectWriteEvent()
    {
        $user = new External\Classes\User();

        $counter = 0;
        $user->registerEvent('write', 'name', function() use (&$counter){
            $counter++;
        });

        $user->name = new \StdClass();
        $this->assertEquals(1, $counter);

        // Random read
        $user->name;

        $this->assertEquals(1, $counter);

        $user->name = 'backToAString';
        $this->assertEquals(2, $counter);
    }

    public function testArrayReadEvent()
    {
        $user = new External\Classes\User();
        $counter = 0;

        $firstName = 'LeeRoy';
        $user->name = ['firstName' => $firstName];
        $user->registerEvent('read', 'name', function() use (&$counter){
            $counter++;
        });

        $this->assertSame($firstName, $user->name['firstName']);
        $this->assertEquals(1, $counter);

        $user->name['firstName'];
        $this->assertEquals(2, $counter);
    }

    public function testArrayWriteEvent()
    {
        $user = new External\Classes\User();

        $counter = 0;
        $user->registerEvent('write', 'name', function() use (&$counter){
            $counter++;
        });

        $firstName = 'LeeRoy';
        $user->name = ['firstName' => $firstName];
        $this->assertEquals(1, $counter);
    }

//    public function testArrayWithOffsetWriteEvent()
//    {
//        $this->markTestSkipped('Need to implement ArrayAccess OffSetGet/Set to hook into these');
//        $user = new External\Classes\User();
//
//        $counter = 0;
//        $user->on('write')->of('name')->run(function() use (&$counter){
//            $counter++;
//        });
//
//        $firstName = 'LeeRoy';
//        $user->name['firstName'] = $firstName;
//        $this->assertEquals(1, $counter);
//    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenReadingReservedEventProperty()
    {
        $user = new External\Classes\User();
        $user->_ventEvents;
    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenWritingReservedEventProperty()
    {
        $user = new External\Classes\User();
        $user->_ventEvents = '123';
    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenReadingReservedVariableProperty()
    {
        $user = new External\Classes\User();
        $user->_ventVariables;
    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenWritingReservedVariableProperty()
    {
        $user = new External\Classes\User();
        $user->_ventVariables = '123';
    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenReadingReservedRegisteredProperty()
    {
        $user = new External\Classes\User();
        $user->_ventRegistered;
    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenWritingReservedRegisteredProperty()
    {
        $user = new External\Classes\User();
        $user->_ventRegistered = '123';
    }


    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenReadingReservedFunctionsProperty()
    {
        $user = new External\Classes\User();
        $user->_ventFunctions;
    }

    /**
     * @expectedException \Exception
     */
    public function testExceptionThrownWhenWritingReservedFunctionsProperty()
    {
        $user = new External\Classes\User();
        $user->_ventFunctions = '123';
    }

}