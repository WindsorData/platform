require 'json' # poner en superclase

class SearchController < ApplicationController
  before_filter :authenticate_user!
  before_filter :find_groups, only: [:quick_search, :full_search]

  def quick_search
    authorize!(:perform, :quick_search)
  end  
  def full_search
    @roles = Role.all
    # @cash_comp_values = JSON.parse(RestClient.get('http://192.168.161.176:9000/schema/values/cashCompensations'))
  end

  def results
    params_hash = params.except(:controller, :action, :authenticity_token, :utf8, :role_form)
    json_query = to_json_query(params_hash)
  end

  private
  def find_groups
    @groups = current_user.is_client? ? Group.by_company(current_user.company) : @groups = Group.all
  end

  def to_json_query(params_hash)
    # Example:
    # params_hash = {
    #   "role_form_1"=> {
    #     "role"=>"1",
    #     "pay_rank"=>"CTO",
    #     "cash_comp"=>{"type"=>"Salary", "gt"=>"1", "lt"=>"3"},
    #     "eq_comp"=>{"type"=>"Salary", "gt"=>"10"}
    #   },
    #   "role_form_2"=> {
    #     "role"=>"1",
    #     "pay_rank"=>"CEO",
    #     "cash_comp"=>{"type"=>"Salary", "gt"=>"4", "lt"=>"10"},
    #     "eq_comp"=>{"type"=>"Salary", "gt"=>"13"}
    #   }
    # }


    a = Hash.new
    a[:basics] = Array.new
    params_hash.each_with_index { |(k1, v1), i1|
      a[:basics][i1] = Hash[filters: Array.new([])]

      params_hash[k1].each_with_index { |(k2, v2), i2|
        if params_hash[k1][k2].is_a? Hash
          if params_hash[k1][k2]['gt'] && params_hash[k1][k2]['lt']
            hash = Hash[field: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']]] ) ]
          elsif params_hash[k1][k2]['gt']
            hash = Hash[field: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt']]])]
          elsif params_hash[k1][k2]['lt']
            hash = Hash[field: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: Array.new( [Hash[lt: params_hash[k1][k2]['lt']]])]
          end            
        else
          hash = Hash[field: k2, value: params_hash[k1][k2]]
        end
        a[:basics][i1][:filters][i2] = hash
      }

    }
    json_query = a.to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
    # Result:
    # {
    #   'basics':
    #   [
    #     {
    #       'filters':[
    #         {'field':'role','value': 1},
    #         {'field':'pay_rank','value':'CTO'},
    #         {'field':'cash_comp.salary','operators':[{'gt': 1,'lt': 3}]},
    #         {'field':'eq_comp.salary','operators':[{'gt':'10'}]}
    #       ]
    #     },
    #     {
    #       'filters':[
    #         {'field':'role','value': 1},
    #         {'field':'pay_rank','value':'CEO'},
    #         {'field':'cash_comp.salary','operators':[{'gt': 4,'lt':'10'}]},
    #         {'field':'eq_comp.salary','operators':[{'gt':'13'}]}
    #       ]
    #     }
    #   ]
    # }

  end
end