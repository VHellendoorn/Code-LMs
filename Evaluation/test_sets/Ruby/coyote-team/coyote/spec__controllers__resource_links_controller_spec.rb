# frozen_string_literal: true

# == Schema Information
#
# Table name: resource_links
#
#  id                  :bigint           not null, primary key
#  subject_resource_id :bigint           not null
#  verb                :string           not null
#  object_resource_id  :bigint           not null
#  created_at          :datetime         not null
#  updated_at          :datetime         not null
#
# Indexes
#
#  index_resource_links                         (subject_resource_id,verb,object_resource_id) UNIQUE
#  index_resource_links_on_object_resource_id   (object_resource_id)
#  index_resource_links_on_subject_resource_id  (subject_resource_id)
#

RSpec.describe ResourceLinksController do
  let(:organization) { create(:organization) }
  let(:subject_resource) { create(:resource, organization: organization) }
  let(:object_resource) { create(:resource, organization: organization) }
  let(:resource_link) do
    create(:resource_link, verb: "hasPart", subject_resource: subject_resource, object_resource: object_resource)
  end

  let(:resource_link_params) do
    {
      id:              resource_link.id,
      organization_id: organization,
    }
  end

  let(:new_resource_link_params) do
    {
      organization_id: organization,
      resource_link:   {
        subject_resource_id: subject_resource.id,
        verb:                "isVersionOf",
        object_resource_id:  object_resource.id,
      },
    }
  end

  let(:update_resource_link_params) do
    resource_link_params.merge(resource_link: {verb: "hasFormat"})
  end

  describe "as a signed-out user" do
    include_context "with no user signed in"

    it "requires login for all actions" do
      aggregate_failures do
        get :show, params: resource_link_params
        expect(response).to require_login

        get :edit, params: resource_link_params
        expect(response).to require_login

        get :new, params: {organization_id: organization}
        expect(response).to require_login

        post :create, params: new_resource_link_params
        expect(response).to require_login

        patch :update, params: update_resource_link_params
        expect(response).to require_login

        delete :destroy, params: resource_link_params
        expect(response).to require_login
      end
    end
  end

  describe "as an author" do
    include_context "with a signed-in author user"

    it "succeeds for basic actions" do
      get :show, params: resource_link_params
      expect(response).to be_successful

      expect {
        get :edit, params: resource_link_params
      }.to raise_error(Pundit::NotAuthorizedError)

      get :new, params: {organization_id: organization, subject_resource_id: subject_resource.id}
      expect(response).to be_successful

      expect {
        post :create, params: new_resource_link_params
        expect(response).to be_redirect
      }.to change(subject_resource.subject_resource_links, :count).by(1)

      bad_params = {
        organization_id: organization,
        resource_link:   {
          subject_resource_id: subject_resource.id,
          verb:                "",
          object_resource_id:  object_resource.id,
        },
      }

      post :create, params: bad_params
      expect(response).not_to be_redirect

      expect {
        patch :update, params: update_resource_link_params
      }.to raise_error(Pundit::NotAuthorizedError)

      expect {
        delete :destroy, params: update_resource_link_params
      }.to raise_error(Pundit::NotAuthorizedError)
    end
  end

  describe "as an editor" do
    include_context "with a signed-in editor user"

    it "succeeds for critical actions" do
      get :new, params: {organization_id: organization, subject_resource_id: subject_resource.id}
      expect(response).to be_successful

      get :edit, params: resource_link_params
      expect(response).to be_successful

      expect {
        patch :update, params: update_resource_link_params
        expect(response).to redirect_to(resource_link)
        resource_link.reload
      }.to change(resource_link, :verb).to("hasFormat")

      bad_params = update_resource_link_params.dup
      bad_params[:resource_link][:verb] = ""

      patch :update, params: bad_params
      expect(response).not_to be_redirect

      expect {
        delete :destroy, params: resource_link_params
        expect(response).to redirect_to(subject_resource)
      }.to change { ResourceLink.exists?(resource_link.id) }
        .from(true).to(false)
    end
  end
end
