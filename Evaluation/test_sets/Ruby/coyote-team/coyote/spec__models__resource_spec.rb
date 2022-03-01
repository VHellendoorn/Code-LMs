# frozen_string_literal: true

# == Schema Information
#
# Table name: resources
#
#  id                    :bigint           not null, primary key
#  host_uris             :string           default([]), not null, is an Array
#  is_deleted            :boolean          default(FALSE), not null
#  name                  :string           default("(no title provided)"), not null
#  priority_flag         :boolean          default(FALSE), not null
#  representations_count :integer          default(0), not null
#  resource_type         :enum             default("image"), not null
#  source_uri            :citext           not null
#  source_uri_hash       :string
#  status                :enum             default("active"), not null
#  created_at            :datetime         not null
#  updated_at            :datetime         not null
#  canonical_id          :citext
#  organization_id       :bigint           not null
#
# Indexes
#
#  index_resources_on_canonical_id                      (canonical_id)
#  index_resources_on_is_deleted                        (is_deleted)
#  index_resources_on_organization_id                   (organization_id)
#  index_resources_on_organization_id_and_canonical_id  (organization_id,canonical_id) UNIQUE
#  index_resources_on_priority_flag                     (priority_flag)
#  index_resources_on_representations_count             (representations_count)
#  index_resources_on_schemaless_source_uri             (source_uri) USING gin
#  index_resources_on_source_uri                        (source_uri)
#  index_resources_on_source_uri_and_organization_id    (source_uri,organization_id) UNIQUE WHERE ((source_uri IS NOT NULL) AND (source_uri <> ''::citext))
#
# Foreign Keys
#
#  fk_rails_...  (organization_id => organizations.id) ON DELETE => restrict ON UPDATE => cascade
#

require "webmock/rspec"

RSpec.describe Resource do
  subject { resource }

  let(:resource) { build(:resource, canonical_id: "abc123", name: "Mona Lisa", source_uri: source_uri) }
  let(:source_uri) { "http://example.com/100.jpg" }

  it { is_expected.to validate_presence_of(:resource_type) }

  specify { expect(resource.label).to eq("Mona Lisa (abc123)") }

  specify do
    expect(resource).to have_many(:subject_resource_links).class_name(:ResourceLink).with_foreign_key(:subject_resource_id).inverse_of(:subject_resource)
  end

  specify do
    expect(resource).to have_many(:object_resource_links).class_name(:ResourceLink).with_foreign_key(:object_resource_id).inverse_of(:object_resource)
  end

  specify do
    expect(resource).to be_viewable
  end

  describe "without the presence of a source URI" do
    let(:resource) { build(:resource, source_uri: "") }

    specify do
      expect(resource).not_to be_viewable
    end
  end

  describe "#related_resources" do
    # this test requires the database as rspec stubs don't completely replicate has_many behaviors
    let!(:resource_link) do
      create(:resource_link, verb: "hasPart")
    end

    let(:subject_resource) { resource_link.subject_resource }
    let(:object_resource) { resource_link.object_resource }

    it "returns correctly labeled predicates" do
      expect(subject_resource.related_resources).to eq([["hasPart", resource_link, object_resource]])
      expect(object_resource.related_resources).to eq([["isPartOf", resource_link, subject_resource]])
    end
  end

  describe "::has_approved_representations" do
    let!(:approved_resource) { create(:resource) }

    before do
      create(:representation, resource: approved_resource, status: "approved")
      create(:resource)
      create(:representation, resource: approved_resource, status: "not_approved")
    end

    it "returns resources that have approved represents" do
      expect(described_class.with_approved_representations).to eq([approved_resource])
    end
  end

  describe "#resource_groups" do
    before { resource.save! }

    it "scopes resource groups to the resource's organization" do
      resource_group = create(:resource_group)
      expect(resource_group.organization).not_to eq(resource.organization)

      expect {
        resource.update(resource_group_ids: [resource_group.id], union_resource_groups: true)
      }.not_to change {
        resource.reload.resource_group_ids
      }.from([resource.resource_group.id])
    end
  end

  describe "#notify_webhook!" do
    include_context "with webhooks"

    it "sends webhook notifications when resources are created" do
      resource = create(:resource, organization: resource_group.organization, resource_groups: [resource_group])
      expect(a_request(:post, "http://www.example.com/webhook/goes/here").with { |req|
        data = JSON.parse(req.body)["data"]
        expect(data).to have_attribute(:canonical_id).with_value(resource.canonical_id)
      }).to have_been_made
    end

    it "doesn't sent webhook notifications in a `without_webhooks` context" do
      described_class.without_webhooks { create(:resource, resource_groups: [resource_group]) }
      expect(a_request(:post, "http://www.example.com/webhook/goes/here")).not_to have_been_made
    end

    it "adds a JWT token header with the resource ID in it" do
      create(:resource, organization: resource_group.organization, resource_groups: [resource_group])
      expect(a_request(:post, "http://www.example.com/webhook/goes/here").with { |req|
        data = JSON.parse(req.body)["data"]
        payload = {id: data["id"]}
        token = JWT.encode(payload, resource_group.token, "HS256")
        expect(req.headers["X-Coyote-Token"]).to eq(token)
      }).to have_been_made
    end
  end

  describe "receiving representations in attributes", clean_db: true do
    let(:organization) { create(:organization) }
    let!(:user) { create(:membership, organization: organization).user }
    let!(:user_2) { create(:membership, organization: organization).user }

    let!(:license) { License.find_by(name: attributes_for(:license, :universal)[:name]) || create(:license, :universal) }
    let!(:license_2) { create(:license, :attribution_international) }

    let(:metum) { organization.meta.find_by!(name: "Alt") }
    let(:metum_2) { organization.meta.find_by!(name: "Long") }

    let(:representation_attributes) { attributes_for(:representation).except(:author, :license, :metum, :resource) }

    describe "on new records" do
      let(:resource) {
        create(:resource,
          organization:               organization,
          representations_attributes: [representation_attributes])
      }
      let(:representation) { resource.representations.first }

      it "creates representations for the resource" do
        expect(resource).to be_persisted
        expect(representation.attributes.symbolize_keys).to include(representation_attributes)
      end

      it "automatically assigns the first active organization user" do
        expect(representation.author).to eq(user)
      end

      it "accepts an author_id" do
        representation_attributes[:author_id] = user_2.id
        expect(representation.author).to eq(user_2)
      end

      it "automatically assigns the universal license" do
        expect(representation.license).to eq(license)
      end

      it "accepts a license by name" do
        representation_attributes[:license] = license_2.name
        expect(representation.license).to eq(license_2)
      end

      it "accepts a license by ID" do
        representation_attributes[:license_id] = license_2.id
        expect(representation.license).to eq(license_2)
      end

      it "automatically assigns the 'Short' metum" do
        expect(representation.metum).to eq(metum)
      end

      it "accepts a metum by name" do
        representation_attributes[:metum] = metum_2.name
        expect(representation.metum).to eq(metum_2)
      end

      it "accepts a metum by ID" do
        representation_attributes[:metum_id] = metum_2.id
        expect(representation.metum).to eq(metum_2)
      end
    end
  end

  describe "#complete?" do
    let(:organization) { resource.organization }

    let!(:required_metum) { create(:metum, is_required: true, organization: organization) }
    let!(:other_required_metum) { create(:metum, is_required: true, organization: organization) }
    let!(:unrequired_metum) { create(:metum, is_required: false, organization: organization) }

    it "returns `true` if the resource has representations for all required meta" do
      # No matter how many UN-required representations you create, it's not complete
      create_list(:representation, 2, metum: unrequired_metum, resource: resource)
      expect(described_class.find(resource.id).complete?).to eq(false)

      # If you've created a representation for SOME of the required meta, it's still incomplete
      create(:representation, metum: organization.meta.find_by!(name: "Alt"), resource: resource)
      create(:representation, metum: required_metum, resource: resource)
      expect(described_class.find(resource.id).complete?).to eq(false)

      # Once you've created a representation for ALL of the required meta, it's complete!!
      create(:representation, metum: other_required_metum, resource: resource)
      expect(described_class.find(resource.id).complete?).to eq(true)
    end
  end

  describe "::find_or_initialize_by_canonical_id_or_source_uri" do
    let!(:resource) { create(:resource, canonical_id: "12345", source_uri: "https://www.example.com/example.jpg") }
    let!(:other_resource) { create(:resource, canonical_id: "67890", source_uri: "https://www.example.com/example%20copy.jpg") }

    it "returns resources with a matching source URI" do
      expect(described_class.find_or_initialize_by_canonical_id_or_source_uri(source_uri: resource.source_uri)).to eq(resource)
    end

    it "returns resources with a matching, schema-less source URI" do
      expect(described_class.find_or_initialize_by_canonical_id_or_source_uri(source_uri: resource.source_uri.gsub(/^https?:\/\//, "//"))).to eq(resource)
    end

    it "returns resources with both a matching canonical ID and a matching source URI" do
      expect(described_class.find_or_initialize_by_canonical_id_or_source_uri(canonical_id: resource.canonical_id, source_uri: resource.source_uri)).to eq(resource)
    end

    it "prefers finding by canonical_id when the source_uri also exists" do
      expect(described_class.find_or_initialize_by_canonical_id_or_source_uri(canonical_id: resource.canonical_id, source_uri: other_resource.source_uri)).to eq(resource)
    end

    it "builds a new resource without a matching source URI or canonical ID" do
      expect(described_class.find_or_initialize_by_canonical_id_or_source_uri(canonical_id: "98765", source_uri: "https://www.not-example.com")).to be_new_record
    end

    it "finds resources with mis-matching HTTP protocols" do
      expect(described_class.find_or_initialize_by_canonical_id_or_source_uri(source_uri: "http://www.example.com/example.jpg")).to eq(resource)
    end
  end
end
