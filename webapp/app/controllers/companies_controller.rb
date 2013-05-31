class CompaniesController < ApplicationController
  inherit_resources

  def create
    create! { redirect_to new_user_path; return }
  end
end