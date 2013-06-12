require 'spec_helper'

describe Role do
  context "When getting roles from backend" do
    it "Should add new roles to the database" do
      count_before = Role.count
      json = [{ name: "CTO" },{ name: "CFO" }].to_json

      Role.load_json(json)

      count_after = Role.count
      count_after.should == count_before + 2
    end
  end
end