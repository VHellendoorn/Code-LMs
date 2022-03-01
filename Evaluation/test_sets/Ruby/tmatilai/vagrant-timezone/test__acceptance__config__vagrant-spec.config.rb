# frozen_string_literal: true

require_relative '../support/spec_helper.rb'

Vagrant::Spec::Acceptance.configure do |c|
  provider = ENV.fetch('VAGRANT_SPEC_PROVIDER')
  box_path = ENV.fetch('VAGRANT_SPEC_BOX_PATH')

  c.component_paths = [File.expand_path('../vagrant-timezone', __dir__)]
  c.skeleton_paths  = [File.expand_path('../skeletons', __dir__)]

  c.provider(provider, box_path: box_path)
end
