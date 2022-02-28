module Furnace::AVM2::ABC
  class ControlTransferOpcode < Opcode
    define_property :conditional

    attr_accessor :target

    def target_offset
      self.offset + byte_length + body.jump_offset
    end

    def target_offset=(new_offset)
      body.jump_offset = new_offset - offset - byte_length
    end

    def resolve!
      @target = @sequence.opcode_at(target_offset)

      if !@target
        # Probably, we're in the middle of invalid code emitted by this fucking braindead
        # compiler. Do something equally insane.
        @target = self
      end
    end

    def lookup!
      self.target_offset = @target.offset
    end
  end
end