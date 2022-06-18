package study.spring.db.service;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import study.spring.db.domain.Member;
import study.spring.db.repository.MemberRepositoryV2;

/**
 * 애플리케이션에서 커넥션을 유지하는 방법 - 커넥션을 파라미터로 전달해서 같은 커넥션이 사용되도록 유지
 * 풀을 고려한 종료
 */
@RequiredArgsConstructor
@Slf4j
public class MemberServiceV2 {

	private final DataSource dataSource;
	private final MemberRepositoryV2 memberRepository;

	public void accountTransfer(String fromId, String toId, int money) throws SQLException {
		// TODO 트랜잭션을 사용하기 위해 JDBC 기술에 의존 -> 향후에 JDBC를 JPA 같은 다른 기술로 바꾸면 서비스 코드도 모두 변경해야 한다
		final Connection con = dataSource.getConnection();
		try {
			con.setAutoCommit(false); // 트랜잭션 시작
			// 비즈니스 로직
			bizLogic(con, fromId, toId, money);
			con.commit(); // 성공 시 커밋
		} catch (Exception e) {
			con.rollback(); // 실패 시 롤백
			throw new IllegalStateException(e);
		} finally {
			release(con);
		}
	}

	private void bizLogic(final Connection con, final String fromId, final String toId, final int money) throws SQLException {
		final Member fromMember = memberRepository.findById(con, fromId);
		final Member toMember = memberRepository.findById(con, toId);

		memberRepository.update(con, fromId, fromMember.getMoney() - money); // autocommit 상태로 이 update는 반영됨
		validation(toMember);
		memberRepository.update(con, toId, toMember.getMoney() + money);
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
