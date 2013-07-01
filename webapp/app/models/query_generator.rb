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
            if params_hash[k1][k2]['type']
              if params_hash[k1][k2]['gt'] && params_hash[k1][k2]['lt']
                hash = Hash[key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [( [Hash[gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']]] )] ]
              elsif params_hash[k1][k2]['gt']
                hash = Hash[key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [( [Hash[gt: params_hash[k1][k2]['gt']]])]]
              elsif params_hash[k1][k2]['lt']
                hash = Hash[key: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [( [Hash[lt: params_hash[k1][k2]['lt']]])]]
              end
            else
              if params_hash[k1][k2]['gt'] && params_hash[k1][k2]['lt']
                hash = Hash[key: k2, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']]] ) ]
              elsif params_hash[k1][k2]['gt']
                hash = Hash[key: k2, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt']]])]
              elsif params_hash[k1][k2]['lt']
                hash = Hash[key: k2, operators: Array.new( [Hash[lt: params_hash[k1][k2]['lt']]])]
              end
            end     
          else
            hash = Hash[key: k2, value: params_hash[k1][k2]]
          end
          a[:executives][i1][:executivesFilters][i2] = hash
        }
      else # advanced search
        a[:advanced] << Hash[key: k1, values: params_hash[k1]]
      end
    }
    a.keys.each{ |k|
      a.delete(k) if a[k].empty?
    }
    json_query = a.to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
  end
end