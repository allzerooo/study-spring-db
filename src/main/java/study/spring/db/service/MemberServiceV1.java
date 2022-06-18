package study.spring.db.service;

import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import study.spring.db.domain.Member;
import study.spring.db.repository.MemberRepositoryV1;

@RequiredArgsConstructor
public class MemberServiceV1 {

	private final MemberRepositoryV1 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		final Member fromMember = memberRepository.findById(fromId);
		final Member toMember = memberRepository.findById(toId);

		memberRepository.update(fromId, fromMember.getMoney() - money); // autocommit 상태로 이 update는 반영됨
		validation(toMember);
		memberRepository.update(toId, toMember.getMoney() + money);
	}

	private void validation(final Member toMember) {
		if (toMember.getMemberId().equals("ex")) {
			throw new IllegalStateException("이체 중 예외 발생");
		}
	}

}
