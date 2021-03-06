package com.ssafy.nnd.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;


public class MemberBoardCustomRepositoryImpl implements MemberBoardCustomRepository {
	
	@Autowired
	EntityManager entityManager;
	
	@Override
	@Query(nativeQuery = true)
	public List findMemberBoardList(List query, List category, List skills, Long mno, Long mode, final Pageable pageable) {
		System.out.println("custom findMemberBoardList");
		StringBuilder str = new StringBuilder();
		str.append("select m.boardno as boardno,idx,email,title,content,category,techstack,"
				+ "m.createdate as createdate,likecnt,name,likeno,mboard,mno,imageurl,profile ");
		str.append("from (memberboard m left join (select likeno, mboard, mno from likemember where mno = " + mno + ") l on boardno = mboard) natural join member ");
		
		str.append("where ");
		if (mode == 2) {
			str.append("mno = " + mno + " and ");
		}
		
		// query
		// title과 content를 대상으로 검색
		if (query.size() > 0) {
			str.append("(");
			for (String string : (List<String>)query) {
				str.append("title like " + "\'%" + string + "%\'");
				str.append(" OR ");
				str.append("content like " + "\'%" + string + "%\'");
				str.append(" OR ");
				str.append("name like " + "\'%" + string + "%\'");
				str.append(" OR ");
			}
			str.append("1 = 0) AND ");
		}
		
		// category
		if (category.size() > 0) {
			str.append("(");
			for (String string : (List<String>)category) {
				str.append("category = " + "\'" + string + "\'");
				str.append(" OR ");
			}
			str.append("1 = 0) AND ");
		}
		
		// skills
		if (skills.size() > 0) {
			str.append("(");
			for (String string : (List<String>)skills) {
				str.append("techstack like " + "\'%" + string + "%\'");
				str.append(" OR ");
			}
			str.append("1 = 0) AND ");
		}
		
		// 맨마지막은 항상 1을 붙여서 AND로 종료되지 않도록 한다.
		str.append("1 = 1 ");
		str.append("order by createdate desc");
		
		System.out.println(str.toString());
		
		int pageNumber = pageable.getPageNumber();
		int pageSize = pageable.getPageSize();
		
		List<Tuple> temp = entityManager.createNativeQuery(str.toString(), Tuple.class).setFirstResult((pageNumber) * pageSize).setMaxResults(pageSize).getResultList();
		String[] keys = {"boardno","idx","email","title","content","category","techstack","createdate","likecnt","name","likeno","mboard","mno","imageurl", "profile"}; 
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < temp.size(); i++) {
			Map<String, Object> real = new HashMap<String, Object>();
			Tuple t = temp.get(i);
			for (int j = 0; j < keys.length; j++) {
				real.put(keys[j], t.get(keys[j]));
			}
			result.add(real);
		}
		return result;
	}


}
