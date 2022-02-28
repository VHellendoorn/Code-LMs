require 'libvirt/spec/network/host'
require 'libvirt/spec/network/range'

module Libvirt
  module Spec
    class Network
      # Represents the DHCP specification which is part of IP.
      class DHCP
        include Util

        attr_accessor :ranges
        attr_accessor :hosts

        # Initializes the DHCP specification. This should never be called
        # directly. Instead, use the {Libvirt::Spec::Network} spec which
        # contains an `ip` attribute which itself contains a `dhcp` attribute.
        def initialize(xml=nil)
          @ranges = []
          @hosts = []

          load!(xml) if xml
        end

        # Loads the DHCP information from the given XML string.
        def load!(root)
          root = Nokogiri::XML(root).root if !root.is_a?(Nokogiri::XML::Element)

          try(root.xpath("//dhcp")) do |dhcp|
            try(dhcp.xpath("range"), :multi => true) do |results|
              self.ranges = []
              results.each { |result| self.ranges << Range.new(result) }
            end

            try(dhcp.xpath("host"), :multi => true) do |results|
              self.hosts = []
              results.each { |result| self.hosts << Host.new(result) }
            end

            raise_if_unparseables(dhcp.xpath("*"))
          end
        end

        # Returns the XML for this specification.
        #
        # @return [String]
        def to_xml(parent=Nokogiri::XML::Builder.new)
          parent.dhcp do |dhcp|
            ranges.each { |r| r.to_xml(dhcp) }
            hosts.each { |h| h.to_xml(dhcp) }
          end

          parent.to_xml
        end
      end
    end
  end
end
