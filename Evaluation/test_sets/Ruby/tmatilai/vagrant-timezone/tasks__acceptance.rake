# frozen_string_literal: true

require_relative '../test/acceptance/support/box_helper'

desc 'Run all acceptance tests'
task acceptance: 'acceptance:all'

namespace :acceptance do
  include VagrantTimezone::BoxHelper

  task all: providers

  providers.each do |provider|
    desc "Run all acceptance tests for #{provider}"
    task provider => provider.boxes.map { |box| "#{provider}:#{box}" }

    namespace provider.name do
      provider.boxes.each do |box|
        desc "Run acceptance tests for #{box} on #{provider}"
        task box.name => box.path do
          env = {
            'VAGRANT_SPEC_PROVIDER' => provider.name,
            'VAGRANT_SPEC_BOX_PATH' => box.path.to_s
          }
          cmd = ['bundle', 'exec', 'vagrant-spec', 'test']
          cmd << "--config=#{config_path('vagrant-spec.config.rb')}"

          rake_output_message "Running acceptance tests for #{box} on #{provider}"
          rake_output_message cmd.join(' ')
          system(env, *cmd)
        end

        file box.path => provider.box_dir do
          rake_output_message "Downloading #{box} for #{provider}"
          download(box)
        end

        directory provider.box_dir
      end
    end
  end
end
