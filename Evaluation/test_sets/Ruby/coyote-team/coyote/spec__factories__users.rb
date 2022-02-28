# frozen_string_literal: true

# == Schema Information
#
# Table name: users
#
#  id                   :integer          not null, primary key
#  active               :boolean          default(TRUE)
#  authentication_token :string           not null
#  current_sign_in_at   :datetime
#  current_sign_in_ip   :string
#  email                :citext           default(""), not null
#  encrypted_password   :string           default(""), not null
#  failed_attempts      :integer          default(0), not null
#  first_name           :citext
#  last_name            :citext
#  last_sign_in_at      :datetime
#  last_sign_in_ip      :string
#  locked_at            :datetime
#  organizations_count  :integer          default(0)
#  password_digest      :string
#  sign_in_count        :integer          default(0), not null
#  staff                :boolean          default(FALSE), not null
#  created_at           :datetime         not null
#  updated_at           :datetime         not null
#
# Indexes
#
#  index_users_on_authentication_token  (authentication_token) UNIQUE
#  index_users_on_email                 (email) UNIQUE
#

FactoryBot.define do
  sequence :token do
    SecureRandom.hex(3)
  end

  factory :user do
    authentication_token { generate(:token) }
    first_name { Faker::Name.first_name }
    last_name { Faker::Name.last_name }
    email { Faker::Internet.unique.email }
    password { Faker::Internet.password }

    transient do
      organization { nil }
      role { :guest }
    end

    trait :devise do
      after(:create) do |user, evaluator|
        user.update_attribute(:password_digest, nil)
        user.update_attribute(:encrypted_password, BCrypt::Password.create("d3visep@ss", cost: 10))
      end
    end

    trait :with_membership do
      after(:create) do |user, evaluator|
        create(:membership, user: user, role: evaluator.role)
      end
    end

    trait :staff do
      staff { true }
    end

    after(:create) do |user, evaluator|
      create(:membership, user: user, organization: evaluator.organization, role: evaluator.role) if evaluator.organization
    end
  end
end
