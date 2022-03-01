require './example/require_models'
# require 'ruby-prof'
# require 'allocation_stats'
require 'bixby/bench'

module SerializationBenchmark
  collection_size = 100
  event = EventFactory.build_event
  team  = event.home_team

  event_collection = collection_size.times.map { event }
  team_collection  = collection_size.times.map { EventFactory.home_team }

  puts "\nObject tests:\n"
  Bixby::Bench.run(10_000) do |b|
    b.sample('ApiView Ultra Simple') do
      ApiView::Engine.render(team)
    end

    b.sample('ApiView Simple') do
      EventSummaryApiView.render(event)
    end

    b.sample('ApiView Complex') do
      BasketballEventApiView.render(event)
    end
  end

  puts "\n\nCollection tests:\n"
  Bixby::Bench.run(100) do |b|
    b.sample('ApiView Ultra Simple: Collection') do
      ApiView::Engine.render(team_collection)
    end

    b.sample('ApiView Simple: Collection') do
      EventSummaryApiView.render(event_collection)
    end

    b.sample('ApiView Complex: Collection') do
      BasketballEventApiView.render(event_collection)
    end
  end
end
