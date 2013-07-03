class QueryGenerator

  def self.json_query(params_hash)
    a = {}
    a[:executives] = []
    a[:advanced] = []
    params_hash.each_with_index { |(k1, v1), i1|
      if k1.starts_with? 'role'
        a[:executives][i1] = Hash[executivesFilters: [([])]]
        params_hash[k1].each_with_index { |(k2, v2), i2|
          if params_hash[k1][k2].is_a? Hash
            if !params_hash[k1][k2]['type'].blank?
              if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
                hash = Hash[key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [ Hash[operator: 'gt', value: params_hash[k1][k2]['gt']],
                  Hash[operator: 'lt', value: params_hash[k1][k2]['lt']]] ]
              elsif !params_hash[k1][k2]['gt'].blank?
                hash = Hash[key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [ Hash[operator: 'gt', value: params_hash[k1][k2]['gt']]] ]
              elsif !params_hash[k1][k2]['lt'].blank?
                hash = Hash[key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [ Hash[operator: 'lt', value: params_hash[k1][k2]['lt']]] ]
              end
            else
              if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
                hash = Hash[key: k2, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']]] ) ]
              elsif !params_hash[k1][k2]['gt'].blank?
                hash = Hash[key: k2, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt']]])]
              elsif !params_hash[k1][k2]['lt'].blank?
                hash = Hash[key: k2, operators: Array.new( [Hash[lt: params_hash[k1][k2]['lt']]])]
              end
            end     
          else
            hash = Hash[key: k2, value: params_hash[k1][k2]] unless params_hash[k1][k2].blank?
          end
          a[:executives][i1][:executivesFilters][i2] = hash if hash
        }
      else # advanced search
        if params_hash[k1].is_a? Hash
          params_hash[k1].each{|k2, v2|
            if !params_hash[k1][k2]['gt'].blank? && !params_hash[k1][k2]['lt'].blank?
              hash = Hash[key: k1 + "." + k2, operators: [ Hash[operator: 'gt', value: params_hash[k1][k2]['gt']], Hash[operator: 'lt', value: params_hash[k1][k2]['lt']]] ]
            elsif !params_hash[k1][k2]['gt'].blank?
              hash = Hash[key: k1 + "." + k2, operators: [ Hash[operator: 'gt', value: params_hash[k1][k2]['gt']]] ]
            elsif !params_hash[k1][k2]['lt'].blank?
              hash = Hash[key: k1 + "." + k2, operators: [ Hash[operator: 'lt', value: params_hash[k1][k2]['lt']]] ]
            end
            a[:advanced] << hash
          }
        end
      end
    }
    
    # Clear values
    a.keys.each{ |k|
      a.delete(k) if a[k].empty?
    }
    a[:executives][0][:executivesFilters].delete_if {|x| x.blank? }
    a[:executives][0].delete(:executivesFilters) if a[:executives][0][:executivesFilters].blank?
    a[:executives].delete_if {|x| x.blank?}
    a.delete(:executives) if a[:executives].blank?

    json_query = a.blank? ? "" : a.to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
  end
end