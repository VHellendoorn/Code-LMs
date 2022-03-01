require 'libvirt/spec/domain/clock'
require 'libvirt/spec/domain/memtune'
require 'libvirt/spec/domain/os_booting'

module Libvirt
  module Spec
    # A specification of a domain. This translates directly down to XML
    # which can be used to define and launch domains on a node by libvirt.
    class Domain
      include Util

      attr_accessor :hypervisor
      attr_accessor :name
      attr_accessor :uuid
      attr_accessor :description
      attr_accessor :os
      attr_accessor :memory
      attr_accessor :current_memory
      attr_accessor :memory_backing
      attr_accessor :memtune
      attr_accessor :vcpu
      attr_accessor :features

      attr_accessor :on_poweroff
      attr_accessor :on_reboot
      attr_accessor :on_crash

      attr_accessor :clock
      attr_accessor :devices

      # Initializes a domain specification. If a valid XML string for a domain
      # is given, the it will attempt to be parsed into the structure. This
      # is still very experimental. As such, if there is something which is
      # found which is not parseable, an {Exception::UnparseableSpec} exception
      # will be raised. Catch this and inspect the message for more information.
      #
      # @param [String] xml XML spec to attempt to parse into the structure.
      def initialize(xml=nil)
        @os = OSBooting.new
        @memtune = Memtune.new
        @features = []
        @clock = Clock.new
        @devices = []

        load!(xml) if xml
      end

      # Attempts to load the attributes from an XML specification. **Warning:
      # this will overwrite any already set attributes which exist in the XML.**
      #
      # @param [String] xml XML spec to attempt to parse into the structure.
      def load!(xml)
        root = Nokogiri::XML(xml).root
        try(root.xpath("//domain[@type]"), :preserve => true) { |result| self.hypervisor = result["type"].to_sym }
        try(root.xpath("//domain/name")) { |result| self.name = result.text }
        try(root.xpath("//domain/uuid")) { |result| self.uuid = result.text }
        try(root.xpath("//domain/memory")) { |result| self.memory = result.text }
        try(root.xpath("//domain/currentMemory")) { |result| self.current_memory = result.text }
        try(root.xpath("//domain/vcpu")) { |result| self.vcpu = result.text }

        try(root.xpath("//domain/on_poweroff")) { |result| self.on_poweroff = result.text.to_sym }
        try(root.xpath("//domain/on_reboot")) { |result| self.on_reboot = result.text.to_sym }
        try(root.xpath("//domain/on_crash")) { |result| self.on_crash = result.text.to_sym }

        try(root.xpath("//domain/clock")) { |result| self.clock = Clock.new(result) }
        try(root.xpath("//domain/os")) { |result| self.os = OSBooting.new(result) }

        try(root.xpath("//domain/devices")) do |result|
          self.devices = []

          result.element_children.each do |device|
            self.devices << Device.load!(device)
          end
        end

        try(root.xpath("//domain/features")) do |result|
          self.features = []

          result.element_children.each do |feature|
            self.features << feature.name.to_sym
          end
        end

        raise_if_unparseables(root.xpath("//domain/*"))
      end

      # Returns the XML for this specification. This XML may be passed
      # into libvirt to create a domain. This is actually the method which
      # should be used for validation of this XML, since libvirt has
      # great validation built in. If you define a domain and an error occurs,
      # then it will notify you what is missing or wrong with the specification.
      #
      # @return [String]
      def to_xml
        Nokogiri::XML::Builder.new do |xml|
          xml.domain(:type => hypervisor) do
            # Name and description
            xml.name name if name
            xml.uuid uuid if uuid
            xml.description description if description

            # Operating system boot information
            os.to_xml(xml)

            # Basic resources
            xml.memory memory if memory
            xml.currentMemory current_memory if current_memory
            xml.vcpu vcpu if vcpu

            if memory_backing == :huge_pages
              xml.memoryBacking do
                xml.hugepages
              end
            end

            # Memtune handles whether or not to render itself
            memtune.to_xml(xml)

            if !features.empty?
              xml.features do
                features.each do |feature|
                  xml.send(feature)
                end
              end
            end

            # Lifecycle control
            xml.on_poweroff on_poweroff if on_poweroff
            xml.on_reboot on_reboot if on_reboot
            xml.on_crash on_crash if on_crash

            # Clock
            clock.to_xml(xml)

            # Devices
            if !devices.empty?
              xml.devices do
                devices.map { |d| d.to_xml(xml) }
              end
            end
          end
        end.to_xml
      end
    end
  end
end
