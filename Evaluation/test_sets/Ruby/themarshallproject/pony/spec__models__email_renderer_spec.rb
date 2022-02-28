require 'rails_helper'

RSpec.describe EmailRenderer, type: :model do
  describe "#html" do
    it "renders markdown" do
      email = Email.new(content: "## header\n\n*ital* test")
      renderer = EmailRenderer.new(email: email)
      expect(renderer.content_html).to eq("<h2 id=\"header\">header</h2>\n\n<p><em>ital</em> test</p>\n")
    end
  end
end
