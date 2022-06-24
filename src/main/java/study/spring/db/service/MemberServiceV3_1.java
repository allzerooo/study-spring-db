package study.spring.db.service;

import java.sql.Connection;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import study.spring.db.domain.Member;
import study.spring.db.repository.MemberRepositoryV3;

/**
 * 애플리케이션에서 커넥션을 유지하는 방법 - 트랜잭션 매니저를 사용
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV3_1 {

	private final PlatformTransactionManager transactionManager;
	private final MemberRepositoryV3 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {

		// 트랜잭션 시작
		final TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

		try {
			// 비즈니스 로직
			bizLogic(fromId, toId, money);
			transactionManager.commit(status); // 성공 시 커밋
		} catch (Exception e) {
			transactionManager.rollback(status); // 실패 시 롤백
			throw new IllegalStateException(e);
		}
	}

	private void bizLogic(final String fromId, final String toId, final int money) throws SQLException {
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

	private void release(final Connection con) {
		if (con != null) {
			try {
				con.setAutoCommit(true); // con.close()는 커넥션을 종료하는게 아니라 반납하는거라서 default 상태인 auto commit 상태로 커넥션을 변경 후 close
				con.close();
			} catch (Exception e) {
				log.info("error", e);
			}
		}
	}

}
