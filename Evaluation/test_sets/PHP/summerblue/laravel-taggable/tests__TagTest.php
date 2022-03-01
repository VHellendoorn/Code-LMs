<?php

use EstGroupe\Taggable\Model\Tag;

class TagTest extends TestCase
{
    protected $testTagName  = 'Test';
    protected $testTagNames = ['萌宠', '萌娃', '恶搞', '吐槽'];

    public function setUp()
    {
        parent::setUp();

        Eloquent::unguard();
        $this->artisan('migrate', [
            '--database' => 'testbench',
            '--realpath' => realpath(__DIR__.'/../migrations'),
        ]);
    }

    protected function getEnvironmentSetUp($app)
    {
        $app['config']->set('taggable.tags_table_name', 'tags');
        $app['config']->set('taggable.taggables_table_name', 'taggables');

        $app['config']->set('database.default', 'testbench');
        $app['config']->set('database.connections.testbench', [
            'driver'   => 'sqlite',
            'database' => ':memory:',
            'prefix'   => '',
        ]);
    }

    public function test_instantiation()
    {
        $tag = new Tag();

        $this->assertInternalType('object', $tag);
    }

    public function test_save()
    {
        $tag = $this->creat_test_tag();
        $this->assertInternalType('object', $tag);
    }


    public function test_scopeSuggested()
    {
        $tag     = $this->creat_test_tag();
        $new_tag = Tag::suggested()->first();
        $this->assertSame($this->testTagName, $new_tag->name);
    }

    public function test_deleteUnused()
    {
        $tag          = new Tag();
        $tag->name    = $this->testTagName;
        $tag->count   = 0;
        $tag->suggest = false;
        $tag->save();

        Tag::deleteUnused();

        $this->assertSame([], Tag::all()->toArray());
    }

    public function test_scopeByTagName()
    {
        $tag     = $this->creat_test_tag();
        $new_tag = Tag::byTagName($this->testTagName)->first();
        $this->assertSame($this->testTagName, $new_tag->name);
    }

    public function test_scopeByTagSlug()
    {
        $tag     = $this->creat_test_tag();
        $new_tag = Tag::byTagSlug(strtolower($this->testTagName))->first();
    }

    public function test_scopeByTagNames()
    {
        $this->create_multiple_test_tag();
        $tags = Tag::byTagNames($this->testTagNames)->get()->toArray();
        $this->assertSame(4, count($tags));
    }

    public function test_scopeByTagIds()
    {
        $this->create_multiple_test_tag();
        $tags = Tag::byTagIds([1, 2, 3, 4])->get()->toArray();
        $this->assertSame(4, count($tags));
    }

    public function test_scopeIdsByNames()
    {
        $this->create_multiple_test_tag();
        $tags = Tag::idsByNames($this->testTagNames)->all();
        sort($tags);

        $this->assertSame(['1', '2', '3', '4'], $tags);
    }

    public function creat_test_tag($tagName = '')
    {
        $tag          = new Tag();
        $tag->name    = $tagName ? $tagName : $this->testTagName;
        $tag->suggest = true;
        $tag->save();
        return $tag;
    }

    public function create_multiple_test_tag()
    {
        foreach ($this->testTagNames as $k => $v) {
            $this->creat_test_tag($v);
        }
    }
}
