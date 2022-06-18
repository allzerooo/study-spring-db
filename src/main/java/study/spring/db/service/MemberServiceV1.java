package study.spring.db.service;

import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import study.spring.db.domain.Member;
import study.spring.db.repository.MemberRepositoryV1;

@RequiredArgsConstructor
public class MemberServiceV1 {

	// TODO 구체 클래스에 의존하고 있기 때문에 인터페이스 도입 필요(다른 구현 기술로 손쉽게 변경할 수 있도록)
	private final MemberRepositoryV1 memberRepository;

	// TODO SQLException은 JDBC를 사용할 때 발생하는 예외이기 때문에 Repository에서 해결하도록 변경
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
