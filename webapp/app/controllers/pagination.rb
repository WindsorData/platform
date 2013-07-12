# Mixin that adds to active record the
# default pagination for this app
module Pagination
  class ActiveRecord::Relation
    def paginated(params)
      page(params[:page]).per(15)
    end
  end
end