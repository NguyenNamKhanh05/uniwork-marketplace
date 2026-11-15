interface Props {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, onChange }: Props) {
  if (totalPages <= 1) {
    return null;
  }
  return (
    <div className="pagination">
      <button
        type="button"
        className="button button-plain"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        Trang trước
      </button>
      <span>
        Trang {page + 1} / {totalPages}
      </span>
      <button
        type="button"
        className="button button-plain"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Trang sau
      </button>
    </div>
  );
}
