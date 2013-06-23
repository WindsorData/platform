require 'spec_helper'
require "cancan/matchers"

describe User do
  describe "abilities" do
    subject { ability }
    let(:ability){ Ability.new(user) }

    context "when is a super user" do
      let(:user){ create(:super) }
      it{ should be_able_to(:manage, User) }
      it{ should be_able_to(:upload, :file) }
      it{ should be_able_to(:read, Group) }
      it{ should be_able_to(:create, Group) }      
    end

    context "when is an admin user" do
      let(:user){ create(:admin) }
      it{ should_not be_able_to(:manage, User) }
      it{ should be_able_to(:upload, :file) }
    end

    context "when is a client user" do
      let(:user){ create(:client) }
      let(:other_client){ create(:client) }

      let(:owned_group){ create(:group, user: user, company: user.company) }
      let(:not_owned_group){ create(:group, user: other_client) }
      let(:other_company_group){ create(:group) }
      let(:same_company_group){ create(:group, user: other_client, company: user.company) }

      it{ should be_able_to(:destroy, owned_group) }
      it{ should_not be_able_to(:destroy, not_owned_group) }

      it{ should be_able_to(:read, owned_group) }
      it{ should_not be_able_to(:read, other_company_group) }

      it{ should be_able_to(:create, Group) }
      it{ should_not be_able_to(:manage, User) }
      it{ should_not be_able_to(:upload, :file) }
    end
  end
  
end
