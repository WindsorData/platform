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
    end

    context "when is an admin user" do
      let(:user){ create(:admin) }
      it{ should_not be_able_to(:manage, User) }
      it{ should be_able_to(:upload, :file) }
    end

    context "when is a client user" do
      let(:user){ create(:client) }
      it{ should_not be_able_to(:manage, User) }
      it{ should_not be_able_to(:upload, :file) }
    end
  end
  
end
